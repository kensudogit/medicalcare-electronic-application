'use client';

import { useState, useEffect } from 'react';
import { 
  PlusIcon, 
  MagnifyingGlassIcon,
  PencilIcon,
  TrashIcon 
} from '@heroicons/react/24/outline';

interface MedicalInstitution {
  id: number;
  institutionCode: string;
  institutionName: string;
  institutionType: string;
  address: string;
  phone: string;
  email: string;
  representativeName: string;
  licenseNumber: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export default function InstitutionsPage() {
  const [institutions, setInstitutions] = useState<MedicalInstitution[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingInstitution, setEditingInstitution] = useState<MedicalInstitution | null>(null);

  useEffect(() => {
    fetchInstitutions();
  }, []);

  const fetchInstitutions = async () => {
    try {
      const response = await fetch('/api/medical-institutions');
      if (response.ok) {
        const data = await response.json();
        setInstitutions(data);
      }
    } catch (error) {
      console.error('Error fetching institutions:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredInstitutions = institutions.filter(institution =>
    institution.institutionName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    institution.institutionCode.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleEdit = (institution: MedicalInstitution) => {
    setEditingInstitution(institution);
    setShowModal(true);
  };

  const handleDelete = async (id: number) => {
    if (confirm('この医療機関を削除しますか？')) {
      try {
        const response = await fetch(`/api/medical-institutions/${id}`, {
          method: 'DELETE',
        });
        if (response.ok) {
          fetchInstitutions();
        }
      } catch (error) {
        console.error('Error deleting institution:', error);
      }
    }
  };

  const handleSubmit = async (formData: FormData) => {
    const institutionData = {
      institutionCode: formData.get('institutionCode') as string,
      institutionName: formData.get('institutionName') as string,
      institutionType: formData.get('institutionType') as string,
      address: formData.get('address') as string,
      phone: formData.get('phone') as string,
      email: formData.get('email') as string,
      representativeName: formData.get('representativeName') as string,
      licenseNumber: formData.get('licenseNumber') as string,
    };

    try {
      const url = editingInstitution 
        ? `/api/medical-institutions/${editingInstitution.id}`
        : '/api/medical-institutions';
      
      const method = editingInstitution ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(institutionData),
      });

      if (response.ok) {
        setShowModal(false);
        setEditingInstitution(null);
        fetchInstitutions();
      }
    } catch (error) {
      console.error('Error saving institution:', error);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-lg">読み込み中...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">医療機関管理</h1>
          <p className="mt-2 text-gray-600">
            登録されている医療機関の一覧と管理を行います。
          </p>
        </div>

        {/* Search and Add */}
        <div className="mb-6 flex justify-between items-center">
          <div className="relative">
            <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="医療機関名またはコードで検索..."
              className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <button
            onClick={() => setShowModal(true)}
            className="btn-primary flex items-center"
          >
            <PlusIcon className="h-5 w-5 mr-2" />
            新規登録
          </button>
        </div>

        {/* Institutions Table */}
        <div className="card">
          <div className="overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    機関コード
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    機関名
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    種別
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    代表者
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    ステータス
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    操作
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredInstitutions.map((institution) => (
                  <tr key={institution.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {institution.institutionCode}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {institution.institutionName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {institution.institutionType}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {institution.representativeName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        institution.status === 'ACTIVE' 
                          ? 'bg-green-100 text-green-800' 
                          : 'bg-red-100 text-red-800'
                      }`}>
                        {institution.status === 'ACTIVE' ? '有効' : '無効'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleEdit(institution)}
                          className="text-primary-600 hover:text-primary-900"
                        >
                          <PencilIcon className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(institution.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          <TrashIcon className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Modal */}
        {showModal && (
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
              <div className="mt-3">
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                  {editingInstitution ? '医療機関編集' : '新規医療機関登録'}
                </h3>
                <form onSubmit={(e) => {
                  e.preventDefault();
                  handleSubmit(new FormData(e.currentTarget));
                }}>
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        機関コード
                      </label>
                      <input
                        type="text"
                        name="institutionCode"
                        defaultValue={editingInstitution?.institutionCode}
                        className="input-field"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        機関名
                      </label>
                      <input
                        type="text"
                        name="institutionName"
                        defaultValue={editingInstitution?.institutionName}
                        className="input-field"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        種別
                      </label>
                      <select
                        name="institutionType"
                        defaultValue={editingInstitution?.institutionType}
                        className="input-field"
                        required
                      >
                        <option value="">選択してください</option>
                        <option value="病院">病院</option>
                        <option value="クリニック">クリニック</option>
                        <option value="薬局">薬局</option>
                        <option value="診療所">診療所</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        住所
                      </label>
                      <textarea
                        name="address"
                        defaultValue={editingInstitution?.address}
                        className="input-field"
                        rows={3}
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        電話番号
                      </label>
                      <input
                        type="tel"
                        name="phone"
                        defaultValue={editingInstitution?.phone}
                        className="input-field"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        メールアドレス
                      </label>
                      <input
                        type="email"
                        name="email"
                        defaultValue={editingInstitution?.email}
                        className="input-field"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        代表者名
                      </label>
                      <input
                        type="text"
                        name="representativeName"
                        defaultValue={editingInstitution?.representativeName}
                        className="input-field"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        許可番号
                      </label>
                      <input
                        type="text"
                        name="licenseNumber"
                        defaultValue={editingInstitution?.licenseNumber}
                        className="input-field"
                        required
                      />
                    </div>
                  </div>
                  <div className="mt-6 flex justify-end space-x-3">
                    <button
                      type="button"
                      onClick={() => {
                        setShowModal(false);
                        setEditingInstitution(null);
                      }}
                      className="btn-outline"
                    >
                      キャンセル
                    </button>
                    <button type="submit" className="btn-primary">
                      {editingInstitution ? '更新' : '登録'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
} 